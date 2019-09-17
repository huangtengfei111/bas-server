[
<#list owner_nums![] as item> 
<#if item["owner_num"]?has_content>
"${item["owner_num"]}"<#sep>,</#sep>
</#if>
</#list>
]