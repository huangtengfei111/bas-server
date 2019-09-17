[
<#if linkedHashMap?has_content>
    <#list linkedHashMap as lhmK, lhmV>
    "${lhmK}",
       [
         <#list lhmV as lm>
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
