[
<#list importResults as map>
{
  <#list map?keys as k>
    <#if k == "counter">
    <#assign counterMap = map["counter"]>
    <#list counterMap?keys as ck>
      "${ck}": ${counterMap[ck]!}<#sep>, </#sep>
    </#list>
    <#else>
    "${k}": "${map[k]!}"
    </#if><#sep>, </#sep>
  </#list><#sep>, </#sep>
}
</#list>
]