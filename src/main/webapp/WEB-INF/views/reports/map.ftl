[
<#if oneMap?has_content>
{
<#list oneMap?keys as k>
"${k}": "${oneMap[k]}"<#sep>, </#sep>
</#list>
}
</#if>
]