[
<#list resultMap as map>
{
  <#list map?keys as itemKey>
        "${itemKey}" : "${map[itemKey]}"<#sep>, </#sep>
  </#list>
}<#sep>, </#sep>
</#list>
]