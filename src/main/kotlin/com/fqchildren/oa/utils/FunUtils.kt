package com.fqchildren.oa.utils

import org.apache.commons.io.IOUtils
import org.springframework.core.io.ClassPathResource
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


object FunUtils {

    /**
     * 获取代码路径下的文件文本
     */
    fun getClassPathFile(fileName: String): String {
        val resource = ClassPathResource(fileName);
        val inputStream = resource.inputStream;
        val stringList = IOUtils.readLines(inputStream, "UTF-8");
        val sb = StringBuilder();
        stringList.forEach {
            sb.append(it);
        }
        return sb.toString();
    }

    /**
     * 删除特殊字符
     */
    fun deleteSpecialText(string: String): String {
        val p = Pattern.compile("[`~!@#\$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]")
        val m = p.matcher(string)
        return m.replaceAll("").trim()
    }

    /**
     * 获取指定范围随机数
     */
    fun random(min: Int, max: Int): Int {
        return Random().nextInt(max - min + 1) + min;
    }

    /**
     * 获取指定长度的随机字符串
     */
    fun randomString(length: Int): String {
        val charArray = arrayListOf(    // 元数据
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
        )
        val string = StringBuilder();
        for (i in 0.until(length)) {
            val number = random(0, charArray.size - 1);
            string.append(charArray[number]);
        }
        return string.toString();
    }

    /**
     * 根据名称获取后缀名
     */
    fun getSuffixName(name: String): String? {
        if (name.isEmpty() || !name.contains(".")) {
            return null;
        }
        val index = name.lastIndexOf(".");
        return name.substring(index + 1, name.length);
    }

    /**
     * 取出字符串中的英文字符与数字(去除特殊字符和中文)
     */
    fun getLetterAndNumber(string: String): String {
        val p = Pattern.compile("[^a-zA-Z0-9]")
        val m = p.matcher(string)
        return m.replaceAll("").trim()
    }

    /**
     * 删除磁盘文件
     */
    fun deleteDiskFile(path: String): Boolean {
        val origFile = File(path);
        if (origFile.exists() && origFile.isFile) {
            return origFile.delete();
        }
        return false;
    }

    /**
     * 对象 转 MAP
     */
    fun toMap(obj: Any): MutableMap<String, Any> {
        val reMap = mutableMapOf<String, Any>()
        val fields = obj.javaClass.declaredFields
        for (i in fields.indices) {
            val subField = obj.javaClass.getDeclaredField(fields[i].name)
            subField.isAccessible = true
            reMap[fields[i].name] = subField.get(fields[i])
        }
        return reMap
    }

    /**
     * 将时间戳转为任意字符串格式
     */
    fun formDataTime(format: String, dateTime: Long): String {
        return SimpleDateFormat(format).format(Date(dateTime));
    }

    /**
     * 读取指定路径文件的内容
     */
    fun readFileTxt(path: String): String {
        val sb = StringBuilder();
        val file = File(path);
        if (!file.exists() || file.isDirectory) {
            return sb.toString();
        }
        FileReader(path).use { reader ->
            BufferedReader(reader).use { br ->
                var line: String? = br.readLine()
                while (line != null) {
                    sb.append(line);
                    line = br.readLine()
                }
            }
        }
        return sb.toString();
    }

    /**
     * 写入指定路径文件的内容
     */
    fun writerFileTxt(path: String, value: String, append: Boolean = false) {
        val writeName = File(path)
        if (!writeName.exists() || writeName.isDirectory) {
            writeName.createNewFile()
        }
        FileWriter(writeName).use { writer ->
            BufferedWriter(writer).use { out ->
                if (append) {
                    out.append(value);
                } else {
                    out.write(value);
                }
                out.flush()
            }
        }
    }

}
