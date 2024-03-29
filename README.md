# 业务系统后端程序
```
1. 标准的Spring Boot项目,正式环境使用war包部署,持久层使用MyBatis框架

2. 配置好SpringBoot环境后修改项目配置文件里的数据库连接信息即可运行.(数据库表与初始数据会在第一次运行自动注入)

3. 带有接口文档自动生成,基于Java注解.项目启动后浏览器打开 "/app/docs" 可以看到生成接口的文档,还可以提供原始的JSON格式数据

4. 耦合性非常低的权限控制模块,可以精确的控制到每个接口,但几乎不影响业务代码的开发编码速度

5. 为每个表自动生成CRUD操作,数据库Mapper实体继承"BaseMapper"对象即可

6. 提供完善且前后端完成分离的前端项目(oa-client),关于前端的内容可以前往前端项目地址查看
```

### 相关项目地址
后端 [oa-service](https://github.com/873764182/oa-service)  
前端 [oa-client](https://github.com/873764182/oa-client)

### 项目目录结构
```
包名目录
    base: 一些基础对象和配置对象,让其他对象继承的如BaseMapper,还有Spring的配置信息
    filter: 过滤器,权限控制的核心基于过滤器
    model: 存放一些标准模型对象,如接口相应数据的统一格式
    rest: 接口(控制器)目录,你的接口应该都写在这里
    service: 服务层,除非有一些复杂但是单一的业务,不然不建议单独建立服务层对象
    table: 数据库表的实体对象与操作对象,操作对象的命名规则为实体对象名加Mapper后缀
    utils: 全局工具的集合,包括权限控制与文档生成的工具对象

资源目录
    config: 存放 SpringBoot 配置文件
    data: 存放一些初始数据,如数据库要求的基础数据,文档静态数据,数据库初始脚本文件
    static: 静态资源目录,前后端是分离的,静态的资源尽量转移给前端处理
```

# 开始使用,编写自己的业务代码
```
1. 打开资源目录的/data/sql/目录的V1__data-base.sql文件,编写脚本创建数据库表,写法可以参考写好的脚本
   脚本的执行基于"flywaydb",修改好脚本后需要打开数据库,找到"flyway_schema_history"表,打开
   清空这个表的所有数据,然后部署重启你的程序即可自动创建你新增的表

2. 在代码目录的table包下建立数据库对应的实体与Mapper对象,
   注意: 一个表对应一个实体对象,一个Mapper对象,实体对象的字段必须与数据库脚本中的字段名一致

3. 在代码目录的rest包下新建你的控制器,编写接口.

4. 在你的接口方法上加上文档注解 "@ApiMethod" 对象, 具体使用可以查看已经写好的其他Rest对象.

5. 加入权限控制,只要在你的接口方法上继续加入 "@Permission" 注解即可, 然后你这个方法的访问就会被权限模块约束.

6. 接口处理完成后用 "RestModel" 对象包装数据后返回,这不是一个强制性要求,只是为了接口数据的一致性,不然前端不好处理.

7. 通过"@RequestMapping"标注的接口路径都必须以do,ins,sel,upd,del结尾,如:/user/user.sel代表查询用户.
   这是一个与框架无关的,仅仅是为了约束规范开发组成员编写习惯的控制,如果不喜欢可以在"NameFilter"过滤器中去掉

8. 在项目的/test/目录对应包名下编写接口测试代码,建议一个Rest对象对应一个Apis测试对象.

9. 将第8步测试成功后拿到的接口数据作为示例数据,填写到/data/docs/的data.json文件,文档需要这份数据作为示例数据.
   不填入则自动生成的文档没有示例数据字段.用接口的API全名称作为数据的Key,接口的返回数据作为Value手动填入data.json文件中

10. 修改数据库等配置信息,然后导出war包,上传到配置好的tomcat应用目录即可部署完成
```

# 关于权限模块的说明
```
权限模块需要配合前端(oa-client)项目才能形成一个完整的项目功能闭环

权限系统的设计其实不难,难的是怎么在保证不影响其他项目成员编写业务代码速度的情况下做到合理的权限控制.

当前的权限控制是依赖于每一个接口的,结构主要为4个层次: 
    1. 接口 => 对应实际的后端接口(@Permission标注的接口),你后端编写了多少个接口就会显示多少个接口
    2. 权限 => 基于接口做的一层抽象,可以把一个或者多个实际的接口抽象为一个权限
    3. 角色 => 抽象的角色,角色可以保定到一个或者多个权限
    4. 用户 => 用户绑定角色,用户也可以有多个角色

基于这四个层,可以把每一个用户的权限控制精确到每一个接口
接口层应该是开发者维护的,因为只有开发者知道每个接口是干什么的
开发者将接口抽象为管理者(如:运营人员)能看得懂的形式.
如:开发者将订单相关的多个接口抽象为一个订单管理权限,然后管理者将这个权限赋值给相应的角色即可

但是这样还是不够的,这仅仅是控制了接口的访问与否,不能控制访问的接口数据范围.

具体的数据范围控制,需要有明确的是数据规定与规范才能实现,每个项目功能可能都不一样

我们可以通过部门来划分用户,将数据范围绑定到部门,每个部门有自己的数据范围.判断用户的部门来判断数据范围
```
