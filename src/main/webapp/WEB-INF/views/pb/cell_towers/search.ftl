[
<#list results as map>
{
  <#list map?keys as k>   
    "${k}": "${map[k]!}"<#sep>, </#sep>   
  </#list> 
}<#sep>, </#sep> 
</#list>
]