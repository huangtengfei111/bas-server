[{
<#list resultMap?keys as k>
"${k}": "${resultMap[k]}"<#sep>, </#sep>
</#list>
}]