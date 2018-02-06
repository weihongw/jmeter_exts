select server_id, parameter_token, index, parent_index, value, deleted, sequence from common.server_parameter_value where sequence > ? and server_id = 'rtb01.pre' order by sequence asc;
select datacenter_id, parameter_token, index, parent_index, value, deleted, sequence from common.datacenter_parameter_value where sequence > ? and (datacenter_id = 'MAR') ORDER BY sequence ASC;
select name, id, is_active, is_random, version, selection_id, deleted, sequence from productserver.feed where sequence > ? ORDER BY sequence ASC;
select token, stratey, deleted, sequence from productserver.association where sequence > ? ORDER BY sequence ASC;
select id, name, deleted, sequence from productserver.selection where sequence > ? ORDER BY sequence ASC;
select selection_id, association_token, priority, max_count, origin, deleted, sequence from productserver.selection_association where sequence > ? ORDER BY sequence ASC;
select hostname, id, deleted, sequence from tracking_common.server where sequence > ? ORDER BY sequence ASC;
select token, parent_token, expression, priority, logging, deleted, rates.sequence from tracking_common.device where sequence > ? ORDER BY sequence ASC;
select code_iso, default_conversion_rate, unit, unit_position, default_format, default_locale, deleted, sequence from common.currency where sequence > ? ORDER BY sequence ASC;
select id, token, deleted, sequence from tracking_common.topic where sequence > ? ORDER BY sequence ASC;
select id, domain, topic_id, currency_code_iso, capping_id, deleted, sequence from tracking_common.website where sequence > ? ORDER BY sequence ASC;
select is_active, https_available, id, name, priority, publisher_delay, url_mapping, url_unmapping, permanent, deleted, sequence from tracking_common.network where sequence > ? ORDER BY sequence ASC;
select id, width, height, deleted, sequence from common.format where sequence > ? ORDER BY sequence ASC;
select token, mime, template, default_template, container_mime, container_template from tracking_common.zone_template where sequence > ? ORDER BY sequence ASC;
select id, token, deleted, sequence from tracking_common.tag_type where sequence > ? ORDER BY sequence ASC;
select selection_id, tag_type_id, priority, max_count, excluded, used_for_association, origin, deleted, sequence from productserver.selection_tag_type where sequence > ? ORDER BY sequence ASC;
select content, mime, token, is_redirect, deleted, sequence from tracking_common.tag_template where sequence > ? ORDER BY sequence ASC;
select id, is_active, format_id, https, audit, ps_ref, ps_ref2, deleted, sequence from tracking_common.ads where sequence > ? ORDER BY sequence ASC;
select active, adserve, oi_adserver, capping_id, estimated_ecpm, id, name, optim_banners_ctr, website_id, parent_id, is_default, https, campaign_platform_id, country_ref, is_psa, deleted, sequence from tracking_common.campaign where sequence > ? ORDER BY sequence ASC;