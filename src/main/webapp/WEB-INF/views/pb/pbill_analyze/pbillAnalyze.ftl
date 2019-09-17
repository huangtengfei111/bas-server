[
<#if linkedHashMap?has_content>
    <#list linkedHashMap ? keys as key>
      "${key!""}" , 
       [
         <#list linkedHashMap[key] as lm>
           {
          <#list lm as k, v>
             "${k!""}":"${v!""}"<#sep>,</#sep> 
          </#list>
           }<#sep>,</#sep>    
         </#list>
       ]<#sep>,</#sep>          
    </#list>      
</#if>
]
