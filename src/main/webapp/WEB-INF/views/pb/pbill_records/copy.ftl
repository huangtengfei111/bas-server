[
  <#list items as item>
    {
      "pbill_id" : "${item.id}",
      "total" : "${item.total}"
    }<#sep>,</#sep>
  </#list>
]