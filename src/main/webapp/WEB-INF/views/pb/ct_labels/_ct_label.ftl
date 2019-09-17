{
<#if ct_label.id?has_content>"id" : ${ct_label.id},</#if>
"ct_code" : "${(ct_label.ct_code)!}",
"label" : "${(ct_label.label)!}",
<#if ct_label.labelGroups?has_content>
"label_groups" : [
    <#list ct_label.labelGroups as labelGroup>
      "${labelGroup.name}"<#sep>, </#sep> 
    </#list>
  ],
 </#if>
"marker_color" : "${(ct_label.marker_color)!}",
<#if ct_label.updated_at?has_content>"updated_at" : "${ct_label.updated_at}",</#if>
"memo" : "${(ct_label.memo)!}"
}
