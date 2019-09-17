
[
<#if resultMap?has_content>
    <#list resultMap as key,value>
      {
      "code": "${key}",
      <#list value as key1,value1 >
           "${key1}":"${value1!}"<#sep>,</#sep> 
       </#list>
       }<#sep>,</#sep> 
    </#list>
  
</#if>
]

