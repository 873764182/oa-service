package com.fqchildren.oa.data

import com.fqchildren.oa.table.RegionInfo
import com.fqchildren.oa.table.RegionInfoMapper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
class RegionInit {

    @Autowired
    lateinit var regionMapper: RegionMapper

    @Autowired
    lateinit var regionInfoMapper: RegionInfoMapper

    @Test
    fun getData() {
        println(regionMapper.selectOldData())
    }

    @Test
    fun update() {
        val old = regionMapper.selectOldData()
        old.forEach {
            val ri = RegionInfo()
            ri.uid = it.Id.toString()
            ri.time = 0
            ri.pid = it.ParentId.toString()
            ri.name = it.Name
            ri.shortName = it.ShortName
            ri.fullName = it.MergerName
            ri.englishName = it.Pinyin
            ri.levelType = it.LevelType
            ri.cityCode = it.CityCode
            ri.zipCode = it.ZipCode
            ri.longitude = it.Lng
            ri.latitude = it.Lat
            val result = regionInfoMapper.insert(ri)
            println("result：$result")
        }
    }

    @Test
    fun getNewData() {
        val data = regionInfoMapper.list(RegionInfo().sql())
        val json = GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(data)
        println(json)
    }

}
