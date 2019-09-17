[
<#if coords?has_content>
<#list coords as coord>
  [${coord[0]!}, ${coord[1]!}]<#sep>,</#sep>
</#list>
</#if>
]