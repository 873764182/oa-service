package com.fqchildren.oa.rest

import com.fqchildren.oa.base.BaseRest
import com.fqchildren.oa.model.ParamModel
import com.fqchildren.oa.model.RestModel
import com.fqchildren.oa.utils.FunUtils
import com.fqchildren.oa.utils.UidUtils
import com.fqchildren.oa.utils.docs.ApiClass
import com.fqchildren.oa.utils.docs.ApiMethod
import com.fqchildren.oa.utils.docs.ApiParam
import com.fqchildren.oa.utils.permission.Permission
import com.google.gson.Gson
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartResolver
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@CrossOrigin(origins = ["*"], maxAge = 3600)
@ApiClass(value = "文件管理")
@RestController
@RequestMapping(value = ["/file"])
class FileRest : BaseRest {
    private val logger = LoggerFactory.getLogger(FileRest::class.java)

    @Value(value = "\${oa.path.file}")
    private lateinit var mPath: String

    @Value(value = "\${oa.service.url}")
    private lateinit var mUrl: String

    @Value(value = "\${oa.path.log}")
    private lateinit var mLog: String

    @ApiMethod(value = "文件上传", depict = "上传成功后返回上传后的下载链接",
            params = [
                ApiParam(value = "orig", depict = "旧文件下载链接,将会被删除"),
                ApiParam(value = "file", depict = "FromData文件流")
            ])
    @RequestMapping(value = ["/upload"], method = [RequestMethod.POST])
    fun upload(request: HttpServletRequest): RestModel {
        if (mPath.isEmpty()) {
            return RestModel(code = -1, msg = "系统异常").withLog(this)
        }
        /* 获取原文件路径 */
        fun getOrigUrl(r: HttpServletRequest): String {
            var origUrl = r.getParameter("orig")  // 先从请求体中获取参数
            if (origUrl.isNullOrEmpty()) {
                val url = r.requestURL.toString()
                if (url.contains("?")) {
                    val getParam = url.split("?")[1]
                    if (getParam.contains("=") && getParam.contains("orig")) {
                        origUrl = getParam.split("=")[1]   // 从URL获取参数
                    }
                }
            }
            return origUrl
        }

        /* 删除旧文件 */
        fun deleteOldFile(p: String?) {
            if (p != null && p.isNotEmpty()) {
                val origPath = mPath + p.replace("/", "\\")
                FunUtils.deleteDiskFile(origPath)
            }
        }

        val multipartResolver = CommonsMultipartResolver(request.session.servletContext)
        if (multipartResolver.isMultipart(request)) {
            val multipartRequest: MultipartHttpServletRequest = request as MultipartHttpServletRequest
            val iterator = multipartRequest.fileNames
            if (iterator.hasNext()) {
                val multipartFile = multipartRequest.getFile(iterator.next().toString())
                if (multipartFile == null || multipartFile.isEmpty) {
                    return RestModel(code = -2, msg = "上传文件为空，请检查上传方法").withLog(this)
                }
                val originName = multipartFile.originalFilename.toString()
                var suffix = FunUtils.getSuffixName(originName)
                if (suffix == null || suffix.isNullOrEmpty()) {
                    return RestModel(code = -3, msg = "文件后缀名称错误").withLog(this)
                }
                suffix = FunUtils.getLetterAndNumber(suffix).toLowerCase()
                val fileDirectory: File = File("$mPath\\$suffix")  // 用后缀名作为独立目录
                if (!fileDirectory.exists() || !fileDirectory.isDirectory) { // 不存在或者不是目录
                    if (!fileDirectory.mkdirs()) {
                        return RestModel(code = -4, msg = "系统目录创建失败").withLog(this)
                    }
                }
                val filePath = "${fileDirectory.absolutePath}\\${UidUtils.nextId()}.$suffix"
                multipartFile.transferTo(File(filePath))   // 保存到文件
                deleteOldFile(getOrigUrl(request)) // 删除旧文件
                // 从浏览器访问的路径应该是去掉目录部分 同时将目录分隔符转为网络分隔符
                val tempServiceUrl = filePath.replace("\\", "/")
                val tempUploadPath = mPath.replace("\\", "/")
                var serviceUrl = tempServiceUrl.replace(tempUploadPath, "")
                if (!serviceUrl.startsWith("/")) {
                    serviceUrl = "/$serviceUrl"
                }
                // 返回结果给客户端
                return RestModel(code = 0, msg = "上传成功", data = (mUrl + serviceUrl))
            } else {
                return RestModel(code = -2, msg = "上传文件为空，请检查上传方法").withLog(this)
            }
        } else {
            return RestModel(code = -2, msg = "上传失败，请检查上传方法").withLog(this)
        }
    }

    @Permission
    @ApiMethod(value = "系统日志", depict = "获取系统日志文件列表")
    @RequestMapping(value = ["/logger.sel"], method = [RequestMethod.POST])
    fun logger(@RequestBody json: ParamModel): RestModel {
        val filePath = File(mLog);
        val listName = LinkedHashSet<Map<String, Any>>();
        filePath.listFiles()?.forEach {
            if (it.isFile) {
                listName.add(mapOf(
                        "name" to it.name,
                        "path" to it.absolutePath,
                        "size" to it.length(),
                        "time" to it.lastModified()));
            }
        }
        return RestModel(data = listName.reversed());
    }

    // @Permission 权限约束对GET请求无效
    @ApiMethod(value = "下载日志", depict = "下载系统日志文件",
            params = [
                ApiParam(value = "path", depict = "日志文件路径")
            ])
    @RequestMapping(value = ["/loggerDownload.do"], method = [RequestMethod.GET])
    fun loggerDownload(path: String?, response: HttpServletResponse) {
        fun writerStringAndColse(response: HttpServletResponse, code: Int = 0, msg: String = "") {
            val result = Gson().toJson(RestModel(code, msg));
            response.contentType = "application/json; charset=utf-8";
            response.setContentLength(result.length);
            response.writer.println(result);
            response.outputStream.flush();
            response.outputStream.close();
        }
        if (path.isNullOrEmpty()) {
            writerStringAndColse(response, code = -1, msg = "参数不能为空");
            return;
        }
        if (!path!!.startsWith(mLog)) {
            writerStringAndColse(response, code = -2, msg = "目录非日志目录");
            return;
        }
        val file = File(path);
        if (!file.exists() || file.isDirectory) {
            writerStringAndColse(response, code = -3, msg = "文件不存在");
            return;
        }
        // response.contentType = "application/force-download";    // 设置强制下载不打开 application/x-msdownload, application/octet-stream
        response.contentType = "text/plain";
        response.setContentLengthLong(file.length());   // 设置文件大小
        response.addHeader("Content-Disposition", "attachment;fileName=${path.replace(mLog, "")}"); // 设置文件名
        val buffer = ByteArray(1024);
        val fis = FileInputStream(file);
        val bis = BufferedInputStream(fis);
        val os = response.outputStream;
        var i = bis.read(buffer);
        while (i != -1) {
            os.write(buffer, 0, i);
            i = bis.read(buffer);
        }
        os.flush();
        bis.close();
        fis.close();
    }

    @Permission
    @ApiMethod(value = "删除日志", depict = "删除日志文件",
            params = [
                ApiParam(value = "path", depict = "日志文件路径")
            ])
    @RequestMapping(value = ["/loggerDelete.do"], method = [RequestMethod.POST])
    fun loggerDelete(@RequestBody params: ParamModel): RestModel {
        if (!params.verify("path")) {
            return params.fail()
        }
        val path = params.string("path")
        var flag = false
        if (path.isNotEmpty() && path.startsWith(mLog)) {   // 只能删除日志目录下的文件
            flag = FunUtils.deleteDiskFile(path)
        }
        return RestModel(data = flag)
    }
}