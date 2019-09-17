[
  <#if locked?has_content>
    <#list locked as k,v>
      {
        "login" : "${k!}",
        "blocked_at" : "${v!}"
      }<#sep>,</#sep>
    </#list>
  </#if>
]