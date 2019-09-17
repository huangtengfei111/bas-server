{
<#if license.id?has_content>"id" : ${license.id},</#if>
"host_id" : "${(license.host_id)!}",
"cust_name" : "${(license.holderDetails.name)!}",
"city" : "${(license.holderDetails.city)!}",
"state" : "${(license.holderDetails.state)!}",
"country" : "${(license.holderDetails.country)!}",
"system_sn" : "${(license.system_sn)!}",
"baseboard_info" : "${(license.baseboard_info)!}" ,
"processor_info" : "${(license.processor_info)!}",
"mac_address" : "${(license.mac_address)!}",
"ip_address" : "${(license.ip_address)!}",
"plan" : "${(license.plan)!}" ,
"acct_limit" : "${(license.acct_limit)!}",
"expired_at" : "${(license.expired_at?datetime)!}" ,
"issued_by" : "${(license.issued_by)!}",
"issued_at" : "${(license.issued_at?datetime)!}"
}