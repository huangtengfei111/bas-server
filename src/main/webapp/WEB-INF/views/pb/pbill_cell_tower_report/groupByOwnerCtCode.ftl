[
<#list resultMap as map>
<#if map?has_content>
{
<#list map?keys as k>
"${k}": "${map[k]!1}"<#sep>, </#sep>
</#list>
}
</#if><#sep>, </#sep>
</#list>
]
