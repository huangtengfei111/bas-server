[
	<#if top_inactive_customers?has_content>
	    <#list top_inactive_customers as top_inactive_customer>
	      {
	        "holder":"${top_inactive_customer["holder"]}",
	        "count":"${top_inactive_customer["count"]}"
	      }<#sep>, </#sep>
	    </#list>
	</#if>
]
