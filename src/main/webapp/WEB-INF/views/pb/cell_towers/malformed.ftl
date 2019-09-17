[<#if pbill_records?has_content>
  <#list pbill_records as pbill_record>
    {
      <#if pbill_record.id?has_content>"id" : ${pbill_record.id},</#if>
      "owner_num" : "${pbill_record.owner_num!}",
      "owner_comm_loc" : "${pbill_record.owner_comm_loc!""}",
      "owner_ct_code" : "${pbill_record.owner_ct_code!""}",
      "owner_ci" : "${pbill_record.owner_ci!""}",
      "owner_lac" : "${pbill_record.owner_lac!""}",
      "owner_ct_addr" : "${pbill_record.ownerCtAddr!""}",
      "peer_num" : "${pbill_record.peer_num!}",
      "peer_comm_loc" : "${pbill_record.peer_comm_loc!""}",
      "peer_ct_code" : "${pbill_record.peer_ct_code!""}",
      "peer_ci" : "${pbill_record.peer_ci!""}",
      "peer_lac" : "${pbill_record.peer_lac!""}",
      "peer_ct_addr" : "${pbill_record.peerCtAddr!""}",
      "started_day" : "${pbill_record.started_day!""}"
    }<#sep>,</#sep>
  </#list>
</#if>
]