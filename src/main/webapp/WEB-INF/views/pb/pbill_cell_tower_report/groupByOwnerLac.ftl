[
<#if resultMap?has_content>
{
<#list resultMap?keys as k>
"${k}": "${resultMap[k]}"<#sep>, </#sep>
</#list>
}
</#if>
]
