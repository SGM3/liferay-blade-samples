/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.blade.samples.elastic.connection.internal.connection;

import com.liferay.blade.samples.elastic.connection.ElasticsearchConnection;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.elasticsearch.internal.util.DocumentTypes;
import com.liferay.portal.search.elasticsearch.settings.ElasticsearchConnectionSettings;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Dylan Rebelak
 */
@Component(immediate = true, service = ElasticsearchConnection.class)
public class ElasticsearchConnectionImpl implements ElasticsearchConnection {

	@Override
	public TransportClient getTransportClient() {
		return _client;
	}

	public SearchHits search() {
		SearchResponse response =
			_client.prepareSearch("liferay-20113")
				.setTypes(DocumentTypes.LIFERAY)
				.setSearchType(SearchType.DEFAULT)
				.setQuery(QueryBuilders.matchAllQuery())
				.execute()
				.actionGet();

		return response.getHits();
	}

	public SearchHits search(String query, String... index) {
		SearchResponse response =
			_client.prepareSearch(index)
				.setQuery(query)
				.execute()
				.actionGet();

		return response.getHits();
	}

	@Activate
	protected void activate() {
		_client = createTransportClient();
	}

	protected void addTransportAddress(
			TransportClient transportClient, String transportAddress)
		throws UnknownHostException {

		String[] transportAddressParts = StringUtil.split(
			transportAddress, StringPool.COLON);

		String host = transportAddressParts[0];

		int port = GetterUtil.getInteger(transportAddressParts[1]);

		InetAddress inetAddress = InetAddress.getByName(host);

		transportClient.addTransportAddress(
			new InetSocketTransportAddress(inetAddress, port));
	}

	protected TransportClient createTransportClient() {
		loadSettings();

		TransportClient transportClient =
			TransportClient.builder()
				.settings(_settingsBuilder.build())
				.build();

		for (String transportAddress :
				connectionSettings.getTransportAddresses()) {

			try {
				addTransportAddress(transportClient, transportAddress);
			}
			catch (Exception e) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"Unable to add transport address " + transportAddress,
						e);
				}
			}
		}

		return transportClient;
	}

	@Deactivate
	protected void deactivate(Map<String, Object> properties) {
		if (_client == null) {
			return;
		}

		_client.close();

		_client = null;
	}

	protected void loadSettings() {
		_settingsBuilder.put(
			"cluster.name", connectionSettings.getClusterName());
		_settingsBuilder.put(
			"client.transport.ignore_cluster_name",
			connectionSettings.getClientTransportIgnoreClusterName());
		_settingsBuilder.put(
			"client.transport.nodes_sampler_interval",
			connectionSettings.getClientTransportNodesSamplerInterval());
		_settingsBuilder.put(
			"client.transport.sniff",
			connectionSettings.getClientTransportSniff());
		_settingsBuilder.put(
			"http.enabled", connectionSettings.getHttpEnabled());
		_settingsBuilder.put("node.client", true);
		_settingsBuilder.put("node.data", false);

		String networkBindHost = connectionSettings.getNetworkBindHost();

		if (Validator.isNotNull(networkBindHost)) {
			_settingsBuilder.put("network.bind.host", networkBindHost);
		}

		String networkHost = connectionSettings.getNetworkHost();

		if (Validator.isNotNull(networkHost)) {
			_settingsBuilder.put("network.host", networkHost);
		}

		String networkPublishHost = connectionSettings.getNetworkPublishHost();

		if (Validator.isNotNull(networkPublishHost)) {
			_settingsBuilder.put("network.publish.host", networkPublishHost);
		}

		String transportTcpPort = connectionSettings.getTransportTcpPort();

		if (Validator.isNotNull(transportTcpPort)) {
			_settingsBuilder.put("transport.tcp.port", transportTcpPort);
		}
	}

	@Reference
	protected ElasticsearchConnectionSettings connectionSettings;

	private static final Log _log = LogFactoryUtil.getLog(
		ElasticsearchConnectionImpl.class);

	private TransportClient _client;
	private Settings.Builder _settingsBuilder = Settings.builder();

}