package com.fqchildren.oa.rest

import com.fqchildren.oa.base.BaseRest
import com.fqchildren.oa.model.RestModel
import com.fqchildren.oa.utils.docs.*
import com.fqchildren.oa.utils.permission.PerUtils
import com.fqchildren.oa.utils.permission.Permission
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

@ApiClass(value = "应用程序")
@RestController
@RequestMapping(value = ["/app"])
class AppRest : BaseRest {

    companion object {
        const val apis_pack_name = "com.fqchildren.oa.rest"
    }

    @ApiMethod(value = "应用程序初始化", depict = "测试程序是否运行成功")
    @RequestMapping(value = ["/init.do"])
    fun init(): RestModel {
        return RestModel(code = 0, msg = "success", data = "程序初始化完成")
    }

    @ApiMethod(value = "系统权限接口", depict = "获取系统中全部需要权限控制的列表接口")
    @RequestMapping(value = ["/permissionApis.sel"])
    fun permissionApis(): RestModel {
        return RestModel(code = 0, msg = "获取完成", data = PerUtils.getApiData(apis_pack_name))
    }

    @ApiMethod(value = "获取接口数据", depict = "获取到接口的JSON原始数据，可以自己处理")
    @RequestMapping(value = ["/apis"])
    fun apis(response: HttpServletResponse): Any {
        return PerUtils.getApiData(apis_pack_name)
    }

    @ApiMethod(value = "获取文档页面", depict = "文档的HTML页面形式，可以自己处理，也可以放到浏览器直接显示")
    @RequestMapping(value = ["/docs"])
    fun docs(response: HttpServletResponse): String {
        response.contentType = "text/html; charset=utf-8"
        val publicParams = listOf(
                ApiPub(name = "h-time", place = ApiPlace.Header, type = ApiType.Long, required = true, pio = ApiIo.Input, example = "1557406501686", depict = "当前时间戳"),
                ApiPub(name = "h-id", place = ApiPlace.Header, type = ApiType.String, required = true, pio = ApiIo.Input, example = "819058456", depict = "客户端设备ID"),
                ApiPub(name = "h-type", place = ApiPlace.Header, type = ApiType.String, required = true, pio = ApiIo.Input, example = "pc", depict = "客户端类型"),
                ApiPub(name = "h-ver", place = ApiPlace.Header, type = ApiType.Int, required = true, pio = ApiIo.Input, example = "20190822 ", depict = "客户端版本"),
                ApiPub(name = "h-token", place = ApiPlace.Header, type = ApiType.String, required = true, pio = ApiIo.Input, example = "xxx", depict = "身份Token数据"),
                ApiPub(name = "code", place = ApiPlace.Param, type = ApiType.Int, required = true, pio = ApiIo.Output, example = "0", depict = "业务状态码"),
                ApiPub(name = "msg", place = ApiPlace.Param, type = ApiType.String, required = true, pio = ApiIo.Output, example = "success", depict = "业务状态信息"),
                ApiPub(name = "data", place = ApiPlace.Param, type = ApiType.Other, required = false, pio = ApiIo.Output, example = "{}", depict = "业务数据")
        )
        val docDepict = "这是OA系统的在线API文档，请求方式如未具体指定则 default 为 post 请求"
        return ApiUtils.getApiDataWithHtml(
                packName = apis_pack_name,
                isChild = true,
                docTitle = "在线文档",
                docDepict = docDepict,
                publicParams = publicParams,
                isZip = true)
    }

    @Permission
    @ApiMethod(value = "获取文档页面(加密)", depict = "文档的HTML页面形式，可以直接用iframe显示")
    @RequestMapping(value = ["/docs.sel"])
    fun docsSel(response: HttpServletResponse): String {
        return docs(response)
    }

}
