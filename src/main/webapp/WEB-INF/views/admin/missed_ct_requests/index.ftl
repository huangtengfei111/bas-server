[
	<#if missed_ct_requests?has_content>
	    <#list missed_ct_requests as missed_ct_request>
	      {
	        "code":"${missed_ct_request["code"]}",
	        "count":"${missed_ct_request["count"]}",
	        "min_created_at":"${missed_ct_request["min_created_at"]}",
	        "max_created_at":"${missed_ct_request["max_created_at"]}"
	      }<#sep>, </#sep>
	    </#list>
	</#if>
]