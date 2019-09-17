[
  <#if venNums?has_content>
    <#list venNums as venNum>
      "${venNum.network}"<#sep>,</#sep>
    </#list>

  </#if>
]