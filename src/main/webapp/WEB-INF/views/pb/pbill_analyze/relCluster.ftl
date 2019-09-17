<@content for="ext">
  {
    <#if summary?has_content>
      <#list summary as map>
        <#assign peerNum="${map['peer_num']!}">  
        <#assign count="${map['count']!}">
        "${peerNum}":"${count}" <#sep>, </#sep> 
      </#list> 
    </#if>
  }
</@content>
{
<#if details?has_content>
<#list details as peerNum, prList>
  "${peerNum}": 
  [
    <#if prList?has_content>
      <#list prList as lhmK, lhmV>    
       {
         <#list lhmV as k,v >
             "${k!}":"${v!}"<#sep>,</#sep> 
         </#list>
       }<#sep>,</#sep>
      </#list>       
    </#if>
  ]<#sep>,</#sep>   
</#list>
</#if>
}