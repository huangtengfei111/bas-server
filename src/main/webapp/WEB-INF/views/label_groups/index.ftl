[
  <#if label_groups?has_content>
    <#list label_groups as label_group >
      {
            "id" : "${label_group.id}",
            "name" : "${label_group.name!}"
          }<#sep>,</#sep>
    </#list>
  </#if>
]