{
<#if ven_number.id?has_content>"id" : ${ven_number.id},</#if>
"num" : "${(ven_number.num)!}",
"short_num" : "${(ven_number.short_num)!}",
"network" : "${(ven_number.network)!}",
<#if ven_number.updated_at?has_content>"updated_at" : "${ven_number.updated_at}",</#if>
"label" : "${(ven_number.label)!}",
"pbills": [
<#assign pbills = ven_number.getPbills()! />
<#if pbills?has_content>
  <#list pbills as pbill>
    {
    "owner_name": "${pbill.owner_name!}",
    "owner_num" : "${pbill.owner_num!}"
    }<#sep>,</#sep>
  </#list>
</#if>
  ]
}