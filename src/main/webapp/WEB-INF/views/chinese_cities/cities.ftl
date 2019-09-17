[
  <#if set?has_content>
  {
    <#list set as data>  
      "${(data._1())}": "${(data._2())}"<#sep>, </#sep>
    </#list>
  }
  </#if>
]