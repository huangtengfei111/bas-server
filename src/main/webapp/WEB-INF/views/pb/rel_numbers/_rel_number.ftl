{
<#if rel_number.id?has_content>"id" : ${rel_number.id},</#if>
"num" : "${(rel_number.num)!}",
"short_num" : "${(rel_number.short_num)!}",
"network" : "${(rel_number.network)!}",
<#if rel_number.updated_at?has_content>"updated_at" : "${rel_number.updated_at}",</#if>
"label" : "${(rel_number.label)!}",
"source" : "${(rel_number.source)!}",
"pbills": [
<#assign pbills = rel_number.getPbills()! />
<#if pbills?has_content>
  <#list pbills as pbill>
    {
     "owner_name": "${pbill.owner_name!''}",
     "owner_num" : "${pbill.owner_num!}"
    }<#sep>, </#sep>
  </#list>
</#if>
  ]
}