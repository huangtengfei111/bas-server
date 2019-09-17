[
<#if countMap?has_content>
<#list countMap?keys as k>
{ "${k}": "${countMap[k]}"}<#sep>, </#sep>
</#list>
</#if>
]