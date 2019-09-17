[
	<#if top_active_customers?has_content>
	    <#list top_active_customers as top_active_customer>
	      {
	        "holder":"${top_active_customer["holder"]}",
	        "count":"${top_active_customer["count"]}"
	      }<#sep>, </#sep>
	    </#list>
	</#if>
]
