[
  <#if relNums?has_content>
    <#list relNums as relNum>
      "${relNum.network}"<#sep>,</#sep>
    </#list>

  </#if>
]