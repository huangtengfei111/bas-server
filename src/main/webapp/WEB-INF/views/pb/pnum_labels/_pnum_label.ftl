{
    <#if pnum_label.id?has_content>"id" : ${pnum_label.id},</#if>
    "num" : "${(pnum_label.num)!}",
    "short_num" : "${(pnum_label.short_num)!}",
    "label" : "${(pnum_label.label)!}",
    "label_txt_color" : "${(pnum_label.label_txt_color)!}",
    "label_bg_color" : "${(pnum_label.label_bg_color)!}",
    "color_order": "${pnum_label.color_order!}",
     <#if pnum_label.labelGroups?has_content>
        "label_groups" : [
          <#list pnum_label.labelGroups as labelGroup>
            "${labelGroup.name}"<#sep>, </#sep> 
          </#list>
      ],
     </#if>
    <#if pnum_label.updated_at?has_content>"updated_at" : "${pnum_label.updated_at}",</#if>
    "ptags" : "${(pnum_label.ptags?js_string)!}",
    "source" : "${pnum_label.source!}",
    "memo" : "${(pnum_label.memo)!}"
  }