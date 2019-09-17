[
  <#if ownerNums?has_content>
    <#list ownerNums as ownerNum>
      "${ownerNum}"<#sep>,</#sep>
    </#list>

  </#if>
]