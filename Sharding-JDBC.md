## 数据分片

### 基本概念

 1) 单库,就是一个库    
![](https://psiitoy.github.io/img/blog/sharding/danku.png)

​ 2) 分片(sharding)，分片解决扩展性问题，引入分片，就引入了数据路由和分片键的概念。分表解决的是数据量过大的问题，分库解决的是数据库性能瓶颈的问题。    
![](https://psiitoy.github.io/img/blog/sharding/fenpian.jpg)

​ 3) 分组(group)，分组解决可用性问题，分组通常通过主从复制(replication)的方式实现。(各种可用级别方案单独介绍)    
![](https://psiitoy.github.io/img/blog/sharding/fenzu.jpg)

​ 4) 互联网公司数据库实际软件架构是(大数据量下)：又分片，又分组（如下图）   

![](https://psiitoy.github.io/img/blog/sharding/fenzu+fenpian.jpg)

###数据分片简介和问题

数据分片是按照某个维度将存放在单一数据库中的数据分散地存放至多个数据库或表中以达到提升性能瓶颈以及可用性的效果。
数据分片的拆分方式分为垂直分片和水平分片

#### 垂直分片
![垂直分片](https://shardingsphere.apache.org/document/current/img/sharding/vertical_sharding.png)    
垂直分片:将数据按照业务进行拆分的方式,又称为纵向拆分,核心是专库专用.即多表进行归类存储.将原来在同一个库中的不同表,按照业务的不同存储在不同库中,来减少单库的访问压力. 

优点:可以一定程度上缓解数据量和访问量的压力.   
问题: 垂直分片往往需要对架构和设计进行调整. 这就造成无法及时响应业务要求,同时由于数据热点的问题,也往往无法彻底解决单点问题.

#### 水平分片
![水平分片](https://shardingsphere.apache.org/document/current/img/sharding/horizontal_sharding.png)    
水平分片:是对于单表数据进行拆分.拆分标准也不再基于业务逻辑,而是将字段根据某种规则将数据分散到不同的表或库中.每个分片只存储一部分数据.常见的是根据主键分片.如:奇数放0库,偶数放1库.      

优点:水平分片理论上突破了单机数据量处理的瓶颈,可以无限横向扩展.是解决单机数据量和访问量限制的终极方案.   
缺点:     
 
- 数据库操作更繁重,数据定位繁琐.   
- 关联操作(分组,聚合,分页,排序等跨单元操作)需要特殊处理.   
- 表名称    
> 因为数据库切分是将整体数据,切分为多份存储,这样就产生了逻辑表和真实表的概念.   
> 真实表:就是实际进行数据存储的表.   
> 逻辑表:就是原本数据单个数据库中的表.即真实表的总和.如:真实表可能分为order_0,order_1,那么逻辑表就是order表,它表示了order表未被切分时的状态.   

- 分布式事务,跨库join(查询).    
- 分表操作,如果要动态增加表时,如果采取hash的方式就存在rehash导致的数据迁移

#### 分片和分库分表的关系   
垂直,水平分片是分布式数据库架构方案.分库,分表是实现方式,分库既可以是垂直也可以是水平切分,分表也同样可以有垂直,水平两种切分方式.

#### 读写分离
![读写分离](https://shardingsphere.apache.org/document/current/img/read-write-split/read-write-split.png)

主从数据库是解决数据库单点问题的简单方案,即根据读写操作上区分数据库职责,以降低单一数据库的访问压力.
应用场景:针对于读多写少的系统,通过拆分更新操作和查询操作,来避免数据更新导致的行锁,提高整个系统的查询性能.
一主多从:分散查询请求,进一步提高查询性能.存在单点问题.   
多主多从:提升系统吞吐量,可用性

存在问题:数据不一致.主库之间,主库和从库之间.
> 如:主键一致性问题.为了高可用,会进行多主部署,如果表主键采取auto_increment,且主库双向同步,a库入库数据后,与b库同步数据如果出现问题,b库入库数据后,再进行ab库数据同步就可能存在主键冲突的问题.

![读写分离数据访问情况](https://shardingsphere.apache.org/document/current/img/read-write-split/sharding-read-write-split.png)

- 主从数据库数据一致性问题    
主从数据一致性原理:
主库用于写,更新操作;从库用于数据读取操作.当主从数据库数据同步存在延迟时,就会造成主从数据库的一致性问题.
    主库->写->binlog->IO线程->从库
延迟原因:从库读取binlog时,会串行执行sql,而主库是并行执行sql;主库故障造成未写入binlog    
> 解决方案:    
1 降低从库读取压力.做法:分库,增加从库机器配置,增加从库机器(一主多从),增加服务缓存    
2 直连主库.会造成主存数据库失去意义.
     ShardingSphere在同一个线程且同一个数据库连接进行写操作时,会采取直连主库的方式,避免数据不一致的问题.

### Sharding-JDBC简介:

定位为轻量级Java框架，在Java的JDBC层提供的额外服务。 它使用客户端直连数据库，以jar包形式提供服务，无需额外部署和依赖，可理解为增强版的JDBC驱动，完全兼容JDBC和各种ORM框架。  

- 适用于任何基于JDBC的ORM框架，如：JPA, Hibernate, Mybatis, Spring JDBC Template或直接使用JDBC。   
- 支持任何第三方的数据库连接池，如：DBCP, C3P0, BoneCP, Druid, HikariCP等。   
- 支持任意实现JDBC规范的数据库。目前支持MySQL，Oracle，SQLServer，PostgreSQL以及任何遵循SQL92标准的数据库。      

![Sharding-JDBC](https://shardingsphere.apache.org/document/current/img/sharding-jdbc-brief.png)  

#### 数据分片,读写分离demo展示

#### 配置说明

spring.shardingsphere.datasource.names= #数据源名称，多数据源以逗号分隔

spring.shardingsphere.sharding.tables.<logic-table-name>.actual-data-nodes= #由数据源名 + 表名组成，以小数点分隔。多个表以逗号分隔，支持inline表达式。缺省表示使用已知数据源与逻辑表名称生成数据节点。用于广播表（即每个库中都需要一个同样的表用于关联查询，多为字典表）或只分库不分表且所有库的表结构完全一致的情况

##### 分库策略，缺省表示使用默认分库策略，以下的分片策略只能选其一

##### 用于单分片键的标准分片场景
spring.shardingsphere.sharding.tables.<logic-table-name>.database-strategy.standard.sharding-column= #分片列名称
spring.shardingsphere.sharding.tables.<logic-table-name>.database-strategy.standard.precise-algorithm-class-name= #精确分片算法类名称，用于=和IN。该类需实现PreciseShardingAlgorithm接口并提供无参数的构造器
spring.shardingsphere.sharding.tables.<logic-table-name>.database-strategy.standard.range-algorithm-class-name= #范围分片算法类名称，用于BETWEEN，可选。该类需实现RangeShardingAlgorithm接口并提供无参数的构造器

##### 用于多分片键的复合分片场景
spring.shardingsphere.sharding.tables.<logic-table-name>.database-strategy.complex.sharding-columns= #分片列名称，多个列以逗号分隔
spring.shardingsphere.sharding.tables.<logic-table-name>.database-strategy.complex.algorithm-class-name= #复合分片算法类名称。该类需实现ComplexKeysShardingAlgorithm接口并提供无参数的构造器

##### 行表达式分片策略
spring.shardingsphere.sharding.tables.<logic-table-name>.database-strategy.inline.sharding-column= #分片列名称
spring.shardingsphere.sharding.tables.<logic-table-name>.database-strategy.inline.algorithm-expression= #分片算法行表达式，需符合groovy语法

##### Hint分片策略
spring.shardingsphere.sharding.tables.<logic-table-name>.database-strategy.hint.algorithm-class-name= #Hint分片算法类名称。该类需实现HintShardingAlgorithm接口并提供无参数的构造器

##### 分表策略，同分库策略
spring.shardingsphere.sharding.tables.<logic-table-name>.table-strategy.xxx= #省略

spring.shardingsphere.sharding.tables.<logic-table-name>.key-generator.column= #自增列名称，缺省表示不使用自增主键生成器
spring.shardingsphere.sharding.tables.<logic-table-name>.key-generator.type= #自增列值生成器类型，缺省表示使用默认自增列值生成器。可使用用户自定义的列值生成器或选择内置类型：SNOWFLAKE/UUID/LEAF_SEGMENT
spring.shardingsphere.sharding.tables.<logic-table-name>.key-generator.props.<property-name>= #属性配置, 注意：使用SNOWFLAKE算法，需要配置worker.id与max.tolerate.time.difference.milliseconds属性。若使用此算法生成值作分片值，建议配置max.vibration.offset属性

spring.shardingsphere.sharding.binding-tables[0]= #绑定表规则列表

spring.shardingsphere.sharding.broadcast-tables[0]= #广播表规则列表

spring.shardingsphere.sharding.default-data-source-name= #未配置分片规则的表将通过默认数据源定位
spring.shardingsphere.sharding.default-database-strategy.xxx= #默认数据库分片策略，同分库策略
spring.shardingsphere.sharding.default-table-strategy.xxx= #默认表分片策略，同分表策略
spring.shardingsphere.sharding.default-key-generator.type= #默认自增列值生成器类型，缺省将使用org.apache.shardingsphere.core.keygen.generator.impl.SnowflakeKeyGenerator。可使用用户自定义的列值生成器或选择内置类型：SNOWFLAKE/UUID/LEAF_SEGMENT
spring.shardingsphere.sharding.default-key-generator.props.<property-name>= #自增列值生成器属性配置, 比如SNOWFLAKE算法的worker.id与max.tolerate.time.difference.milliseconds

spring.shardingsphere.sharding.master-slave-rules.<master-slave-data-source-name>.master-data-source-name= #详见读写分离部分
spring.shardingsphere.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[0]= #详见读写分离部分
spring.shardingsphere.sharding.master-slave-rules.<master-slave-data-source-name>.load-balance-algorithm-class-name= #详见读写分离部分
spring.shardingsphere.sharding.master-slave-rules.<master-slave-data-source-name>.load-balance-algorithm-type= #详见读写分离部分

spring.shardingsphere.props.sql.show= #是否开启SQL显示，默认值: false


#### 分片实现原理

SQL解析 => 执行器优化 => SQL路由 => SQL改写 => SQL执行 => 结果归并

![数据分片流程](https://shardingsphere.apache.org/document/current/img/sharding/sharding_architecture_cn.png)

![功能结构](https://shardingsphere.apache.org/document/current/img/config_domain.png)

ShardingDataSourceFactory用于创建分库分表或分库分表+读写分离的JDBC驱动    
MasterSlaveDataSourceFactory用于创建独立使用读写分离的JDBC驱动。   
ShardingRuleConfiguration是分库分表配置的核心和入口，它可以包含多个TableRuleConfiguration和MasterSlaveRuleConfiguration。    
每一组相同规则分片的表配置一个TableRuleConfiguration。如果需要分库分表和读写分离共同使用，每一个读写分离的逻辑库配置一个MasterSlaveRuleConfiguration。     
每个TableRuleConfiguration对应一个ShardingStrategyConfiguration



这里要说的是数据库切分确实可以解决数据库的单点问题,但是它也会带来整体服务切分后的数据库操作的复杂度.

