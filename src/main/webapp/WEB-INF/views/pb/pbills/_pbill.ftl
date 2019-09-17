<#assign venNum = pbill.getVenNumber(case_id)! />
<#assign relNum = pbill.getRelNumber(case_id)! />

{
<#if pbill.id?has_content>"id" : ${pbill.id},</#if>
"owner_name": "${pbill.owner_name!''}",
"owner_num" : "${pbill.owner_num!}",
"call_attribution" : "${(pbill.call_attribution)!}",

<#if venNum?has_content>
"ven_network": "${venNum.network}",
"ven_short_num": "${venNum.short_num}",
</#if>

<#if relNum?has_content>
"rel_network": "${relNum.network}",
"rel_short_num": "${relNum.short_num}",
</#if>

"residence" : "${(pbill.residence)!}", 
"alyz_day_start" : "${(pbill.alyz_day_start)!}",
"alyz_day_end" : "${(pbill.alyz_day_end)!}",
"started_at" : "${(pbill.started_at)!}",
"ended_at" : "${(pbill.ended_at)!}",
"total" : ${(pbill.total)!0},
"peer_num_count" : "${(pbill.peer_num_count)!}",
"outliers": [
   <#assign outliers = pbill.outliers />
   <#list outliers as outlier>
    ${outlier.flaw_type}<#sep>,</#sep>
   </#list>
],
"created_at" : "${(pbill.created_at)!}",
"updated_at" : "${(pbill.updated_at)!}",
"label_groups": [
  <#assign labelGroups = pbill.getLabelGroups(case_id)! />
  <#if labelGroups?has_content>
  <#list labelGroups as labelGroup>
    "${labelGroup.name}"<#sep>,</#sep>
  </#list>
</#if>
]
}