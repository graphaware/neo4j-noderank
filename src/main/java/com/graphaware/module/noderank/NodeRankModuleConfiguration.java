/*
 * Copyright (c) 2013-2016 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.module.noderank;

import com.graphaware.common.policy.inclusion.NodeInclusionPolicy;
import com.graphaware.common.policy.inclusion.RelationshipInclusionPolicy;
import com.graphaware.common.policy.role.AnyRole;
import com.graphaware.common.policy.role.InstanceRolePolicy;
import com.graphaware.common.policy.role.WritableRole;
import com.graphaware.runtime.config.BaseTimerDrivenModuleConfiguration;
import com.graphaware.runtime.policy.all.IncludeAllBusinessNodes;
import com.graphaware.runtime.policy.all.IncludeAllBusinessRelationships;

/**
 * Configuration settings for the {@link NodeRankModule} with fluent interface.
 */
public class NodeRankModuleConfiguration extends BaseTimerDrivenModuleConfiguration<NodeRankModuleConfiguration> {

    private final String rankPropertyKey;
    private final String rankPropertyCounterKey;
    private final NodeInclusionPolicy nodeInclusionPolicy;
    private final RelationshipInclusionPolicy relationshipInclusionPolicy;
    private final int maxTopRankNodes;
    private final double dampingFactor;
    private final boolean respectDirections;

    /**
     * Retrieves the default {@link NodeRankModuleConfiguration}, which includes all (non-internal) nodes and relationships.
     *
     * @return The default {@link NodeRankModuleConfiguration}
     */
    public static NodeRankModuleConfiguration defaultConfiguration() {
        return new NodeRankModuleConfiguration(WritableRole.getInstance(), "nodeRankCounter", "nodeRank", IncludeAllBusinessNodes.getInstance(), IncludeAllBusinessRelationships.getInstance(), 10, 0.85, false);
    }

    /**
     * Construct a new configuration with the given rank property key counter.
     *
     * @param rankPropertyCounterKey key of the property counter written to the ranked nodes.
     * @return new config.
     */
    public NodeRankModuleConfiguration withRankPropertyCounterKey(String rankPropertyCounterKey) {
        return new NodeRankModuleConfiguration(getInstanceRolePolicy(), rankPropertyCounterKey, getRankPropertyKey(), getNodeInclusionPolicy(), getRelationshipInclusionPolicy(), getMaxTopRankNodes(), getDampingFactor(), getDirections());
    }

    /**
     * Construct a new configuration with the given rank property key.
     *
     * @param rankPropertyKey key of the property written to the ranked nodes.
     * @return new config.
     */
    public NodeRankModuleConfiguration withRankPropertyKey(String rankPropertyKey) {
        return new NodeRankModuleConfiguration(getInstanceRolePolicy(), getRankPropertyCounterKey(), rankPropertyKey, getNodeInclusionPolicy(), getRelationshipInclusionPolicy(), getMaxTopRankNodes(), getDampingFactor(), getDirections());
    }

    /**
     * Construct a new configuration with the given node inclusion policy.
     *
     * @param nodeInclusionPolicy The {@link NodeInclusionPolicy} to use for selecting nodes to include in the rank algorithm.
     * @return new config.
     */
    public NodeRankModuleConfiguration with(NodeInclusionPolicy nodeInclusionPolicy) {
        return new NodeRankModuleConfiguration(getInstanceRolePolicy(), getRankPropertyCounterKey(), getRankPropertyKey(), nodeInclusionPolicy, getRelationshipInclusionPolicy(), getMaxTopRankNodes(), getDampingFactor(), getDirections());
    }

    /**
     * Construct a new configuration with the given node inclusion policy.
     *
     * @param relationshipInclusionPolicy The {@link RelationshipInclusionPolicy} for selecting which relationships to follow when crawling the graph.
     * @return new config.
     */
    public NodeRankModuleConfiguration with(RelationshipInclusionPolicy relationshipInclusionPolicy) {
        return new NodeRankModuleConfiguration(getInstanceRolePolicy(), getRankPropertyCounterKey(), getRankPropertyKey(), getNodeInclusionPolicy(), relationshipInclusionPolicy, getMaxTopRankNodes(), getDampingFactor(), getDirections());
    }

    /**
     * Construct a new configuration with the given maximum number of top ranked nodes to remember.
     *
     * @param maxTopRankNodes to remember.
     * @return new config.
     */
    public NodeRankModuleConfiguration withMaxTopRankNodes(int maxTopRankNodes) {
        return new NodeRankModuleConfiguration(getInstanceRolePolicy(), getRankPropertyCounterKey(), getRankPropertyKey(), getNodeInclusionPolicy(), getRelationshipInclusionPolicy(), maxTopRankNodes, getDampingFactor(), getDirections());
    }

    /**
     * Construct a new configuration with the given damping factor.
     *
     * @param dampingFactor new damping factor.
     * @return new config.
     */
    public NodeRankModuleConfiguration withDampingFactor(double dampingFactor) {
        return new NodeRankModuleConfiguration(getInstanceRolePolicy(), getRankPropertyCounterKey(), getRankPropertyKey(), getNodeInclusionPolicy(), getRelationshipInclusionPolicy(), getMaxTopRankNodes(), dampingFactor, getDirections());
    }

    /**
     * Construct a new configuration with the given respect for relationships' directions..
     *
     * @param respectDirections new settings for respecting directions.
     * @return new config.
     */
    public NodeRankModuleConfiguration withDirections(boolean respectDirections) {
        return new NodeRankModuleConfiguration(getInstanceRolePolicy(), getRankPropertyCounterKey(), getRankPropertyKey(), getNodeInclusionPolicy(), getRelationshipInclusionPolicy(), getMaxTopRankNodes(), getDampingFactor(), respectDirections);
    }

    /**
     * Constructs a new {@link NodeRankModuleConfiguration} based on the given configuration details.
     *
     * @param instanceRolePolicy          specifies which role a machine must have in order to run the module with this configuration. Must not be <code>null</code>.
     * @param rankPropertyKey             name of the property written to the ranked nodes.
     * @param nodeInclusionPolicy         The {@link NodeInclusionPolicy} to use for selecting nodes to include in the rank algorithm.
     * @param relationshipInclusionPolicy The {@link RelationshipInclusionPolicy} for selecting which relationships to follow when crawling the graph.
     * @param maxTopRankNodes             maximum number of top ranked nodes to remember.
     */
    private NodeRankModuleConfiguration(InstanceRolePolicy instanceRolePolicy, String rankPropertyCounterKey, String rankPropertyKey, NodeInclusionPolicy nodeInclusionPolicy, RelationshipInclusionPolicy relationshipInclusionPolicy, int maxTopRankNodes, double dampingFactor, boolean respectDirections) {
        super(instanceRolePolicy);

        if (maxTopRankNodes < 0) {
            throw new IllegalArgumentException("Max top ranked nodes must be > 0");
        }

        if (dampingFactor < 0 || dampingFactor > 1.0) {
            throw new IllegalArgumentException("Damping factor must be between 0.0 and 1.0");
        }

        this.rankPropertyKey = rankPropertyKey;
        this.rankPropertyCounterKey = rankPropertyCounterKey;
        this.nodeInclusionPolicy = nodeInclusionPolicy;
        this.relationshipInclusionPolicy = relationshipInclusionPolicy;
        this.maxTopRankNodes = maxTopRankNodes;
        this.dampingFactor = dampingFactor;
        this.respectDirections = respectDirections;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected NodeRankModuleConfiguration newInstance(InstanceRolePolicy instanceRolePolicy) {
        return new NodeRankModuleConfiguration(instanceRolePolicy, getRankPropertyCounterKey(), getRankPropertyKey(), getNodeInclusionPolicy(), getRelationshipInclusionPolicy(), getMaxTopRankNodes(), getDampingFactor(), getDirections());
    }

    public String getRankPropertyCounterKey() {
        return rankPropertyCounterKey;
    }

    public String getRankPropertyKey() {
        return rankPropertyKey;
    }

    public NodeInclusionPolicy getNodeInclusionPolicy() {
        return nodeInclusionPolicy;
    }

    public RelationshipInclusionPolicy getRelationshipInclusionPolicy() {
        return relationshipInclusionPolicy;
    }

    public int getMaxTopRankNodes() {
        return maxTopRankNodes;
    }

    public double getDampingFactor() {
        return dampingFactor;
    }

    public boolean getDirections() {
      return respectDirections;
    }
}
