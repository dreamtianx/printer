# printer


java的控制台打印工具。 

可以打印任何对象，数组，List接口对象，Map接口对象。重点在可以结构化的在控制台显示，就像通过json工具全部展开的json字符串一样。

只需要直接调用P.print(obj) 

对于复杂对象，不支持 list嵌套list 。 map嵌套list 两种情况 遇到这种情况只会显示list的长度。

对于list元素的打印限制在100个，可以有效防止栈溢出。

递归深度 的意思是， A持有B B持有C  算是深度为2 。 默认的深度是3 。 大于深度的时候，不会输出超出深度的对象内部元素。
