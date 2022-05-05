### 设备定义
1. 设备ID
2. 产品ID
3. 产品版本
4. 消息协议
5. 物模型
6. 其他配置

 #### 物模型
(
``{"events":[],"properties":[],"functions":[],"tags":[]}
events:事件定义；properties：配置属性；functions：功能定义；tags：标签；
``
)
1. Properties(属性):一般用于描述设备运行时的状态，如环境监测设备所读取的当前环境温度等。属性支持GET和SET请求方式。应用系统可发起对属性的读取和设置请求。
2. functions(功能): 设备可被外部调用的能力或方法，可设置输入参数和输出参数。相比于属性，功能可通过一条指令实现更复杂的业务逻辑，如执行某项特定的任务。
3. events(事件): 设备运行时的事件。事件一般包含需要被外部感知和处理的通知信息，可包含多个输出参数。如，某项任务完成的信息，或者设备发生故障或告警时的温度等，事件可以被订阅和推送。
``
   {
   "properties":[
   {
   "id":"标识",
   "name":"属性名称",
   "valueType":{
   "min":"参数最小值（int、float、double类型特有）",
   "max":"参数最大值（int、float、double类型特有）",
   "step":"步长，字符串类型",
   "unit":"属性单位",
   "expands":{},//扩展属性
   "type":"属性类型: int（原生）、float（原生）、double（原生）、text（原生）、date（默认String类型UTC毫秒,可以自定义）、bool（0或1的int类型）、enum（int类型）、object（结构体类型，可包含前面6种类型）、array（数组类型，支持int/double/float/String）、file（文件，支持URL[地址]/base64[base64编码]/binary[二进制]）、password（密码）"
   },
   "expands":{
   "readOnly":"是否只读(true/false)",
   "report":"设备是否上报(true/false)"
   },
   "description":"说明"
   }
   ],
   "functions":[
   {
   "id":"标识",
   "name":"功能名称",
   "inputs":[//输入参数
   {
   "id":"输入参数标识",
   "name":"输入参数名称",
   "valueType":{
   "min":"参数最小值（int、float、double类型特有）",
   "max":"参数最大值（int、float、double类型特有）",
   "step":"步长，字符串类型",
   "unit":"属性单位",
   "type":"属性类型: int（原生）、float（原生）、double（原生）、text（原生）、date（默认String类型UTC毫秒,可以自定义）、bool（0或1的int类型）、enum（int类型）、object（结构体类型，可包含前面6种类型）、array（数组类型，支持int/double/float/String）、file（文件，支持URL[地址]/base64[base64编码]/binary[二进制]）、password（密码）"
   }
   }
   ],
   "outputs":{//输出参数
   "min":"参数最小值（int、float、double类型特有）",
   "max":"参数最大值（int、float、double类型特有）",
   "step":"步长，字符串类型",
   "unit":"属性单位",
   "type":"属性类型: int（原生）、float（原生）、double（原生）、text（原生）、date（默认String类型UTC毫秒,可以自定义）、bool（0或1的int类型）、enum（int类型）、object（结构体类型，可包含前面6种类型）、array（数组类型，支持int/double/float/String）、file（文件，支持URL[地址]/base64[base64编码]/binary[二进制]）、password（密码）"
   },
   "isAsync":"是否异步(true/false)",
   "description":"说明"
   }
   ],
   "events":[
   {
   "id":"标识",
   "name":"事件名称",
   "valueType":{
   "min":"参数最小值（int、float、double类型特有）",
   "max":"参数最大值（int、float、double类型特有）",
   "step":"步长，字符串类型",
   "unit":"属性单位",
   "type":"属性类型: int（原生）、float（原生）、double（原生）、text（原生）、date（默认String类型UTC毫秒,可以自定义）、bool（0或1的int类型）、enum（枚举）、object（结构体类型，可包含前面6种类型）、array（数组类型，支持int/double/float/String）、file（文件，支持URL[地址]/base64[base64编码]/binary[二进制]）、password（密码）"
   },
   "expands":{
   "level":"事件级别(普通[ordinary]/警告[warn]/紧急[urgent])",
   "eventType":"事件类型(数据上报[reportData]/事件上报[reportEvent])"
   },
   "description":"说明"
   }
   ]
   }  
``

