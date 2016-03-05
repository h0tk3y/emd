package com.github.h0tk3y.emd

import java.util.*

private const val SOURCE_ID = 0
private const val SINK_ID = 1

/**
 * Earth Mover's Distance implementation of [HistogramDistance].
 * The numeric type is chosen by subclasses.
 *
 * @param TItem type of items in the histograms.
 * @param TNumeric type of numeric values in histograms, also used for the result.
 *        Should have precise arithmetic, otherwise the behavior is undefined.
 *
 * @param distance function that provides the distance between two [TItem] objects.
 *        It should satisfy the three metric axioms, otherwise no correct histogram
 *        distance is guaranteed.
 */
abstract class Emd<TItem, TNumeric>(val distance: ((TItem, TItem) -> TNumeric)?)
: HistogramDistance<TItem, TNumeric>
where TNumeric : Number, TNumeric : Comparable<TNumeric> {

    /** Provides zero of [TNumeric]. */
    abstract protected val zero: TNumeric

    /** Adds two [TNumeric] items. */
    abstract protected infix fun TNumeric.plus(other: TNumeric): TNumeric

    /** Subtracts two [TNumeric] items. */
    abstract protected infix fun TNumeric.minus(other: TNumeric): TNumeric

    /** Multiplies two [TNumeric] items. */
    abstract protected infix fun TNumeric.times(other: TNumeric): TNumeric

    protected inner class Edge(val cost: TNumeric,
                               val capacity: TNumeric,
                               var flow: TNumeric = zero)

    private inner class FlowNetwork(val graph: Map<Int, Map<Int, Edge>>) {

        val maxNode = graph.keys.max()!!
        val infCost = graph.flatMap { it.value.values.map { it.cost } }.fold(zero) { acc, i -> acc plus i plus i }

        private fun cheapestResidualPath(from: Int, to: Int): List<Int>? {
            //prev[k] = v such that edge (v -> k) is optimal
            val prev = HashMap<Int, Int>()

            //m[k] = cheapest path cost
            val m = HashMap<Int, TNumeric>()
            m[from] = zero

            for (i in 0..maxNode) {
                for ((nFrom, es) in graph) {
                    for ((nTo, e) in es) {
                        if (e.flow < e.capacity) {
                            //the edge is in residual network
                            val candidateCost = (m[nFrom] ?: infCost) plus e.cost
                            if (m[nTo] ?: infCost > candidateCost) {
                                m[nTo] = candidateCost
                                prev[nTo] = nFrom
                            }
                        }
                        if (e.flow > zero) {
                            //the reverse edge is in residual network
                            val candidateCost = (m[nTo] ?: (infCost plus e.cost)) minus e.cost
                            if (m[nFrom] ?: infCost > candidateCost) {
                                m[nFrom] = candidateCost
                                prev[nFrom] = nTo
                            }
                        }
                    }
                }
            }


            if (m[to] == null)
                return null

            val result = generateSequence(to) { if (it == from) null else prev[it] }.take(maxNode).toList().reversed()

            if (result.first() != SOURCE_ID || result.last() != SINK_ID)
                return null

            return result
        }

        fun findMaxFlowMinCost() {
            while (true) {
                val path = cheapestResidualPath(SOURCE_ID, SINK_ID)
                           ?: break

                val nodePairs = path.zip(path.drop(1))
                val increase = nodePairs.map {
                    graph[it.first]?.get(it.second)?.let { it.capacity minus it.flow } ?:
                    graph[it.second]?.get(it.first)!!.flow
                }.min()!!

                for ((from, to) in nodePairs) {
                    val direct = graph[from]?.get(to)
                    if (direct != null) {
                        direct.flow = direct.flow plus increase
                    } else {
                        val reverse = graph[to]!![from]!!
                        reverse.flow = reverse.flow minus increase
                    }
                }
            }
        }
    }

    override fun histogramDistance(h1: Map<TItem, TNumeric>,
                                   h2: Map<TItem, TNumeric>): TNumeric {

        val distancesCache = HashMap<Pair<TItem, TItem>, TNumeric>()

        fun distance(t1: TItem, t2: TItem) =
                distancesCache[Pair(t1, t2)] ?:
                distancesCache[Pair(t2, t1)] ?:
                (distance?.invoke(t1, t2) ?:
                 @Suppress("UNCHECKED_CAST") (t1 as DistanceMeasurable<in TItem, TNumeric>) distanceTo t2)
                        .apply {
                            distancesCache[Pair(t1, t2)] = this
                            distancesCache[Pair(t2, t1)] = this
                        }

        //First, we build a bipartite flow network, left part is items from h1, right -- from h2.
        var nextNodeId = SINK_ID + 1
        val mapLeftIds = h1.keys.associateBy({ it }, { nextNodeId++ })
        val mapRightIds = h2.keys.associateBy({ it }, { nextNodeId++ })
        val graph = HashMap<Int, HashMap<Int, Edge>>()

        //add edges from source to the left part nodes, cost = 0, capacity = h1[t]
        val edgesSource = HashMap<Int, Edge>().apply { graph[SOURCE_ID] = this }
        for ((n, v) in mapLeftIds.map { it.value to h1[it.key]!! }) {
            edgesSource[n] = Edge(zero, v)
        }

        //add edges from the right part nodes to sink, cost = 0, capacity = h2[t]
        for ((n, v) in mapRightIds.map { it.value to h2[it.key]!! }) {
            graph[n] = hashMapOf(SINK_ID to Edge(zero, v))
        }

        //add edges between the left and the right part nodes, cost = d(t1, t2), capacity such that all the flow
        //from the left part can go through any edge
        val capacity = h1.values.fold(zero) { acc, it -> acc plus it }.let { it plus it }

        for ((tl, hl) in h1) {
            val nodeLeft = mapLeftIds[tl]!!
            val edges = HashMap<Int, Edge>().apply { graph[nodeLeft] = this }
            for ((tr, hr) in h2) {
                val nodeRight = mapRightIds[tr]!!
                val d = distance(tl, tr)

                edges[nodeRight] = Edge(d, capacity)
            }
        }

        val network = FlowNetwork(graph)
        network.findMaxFlowMinCost()

        return graph.values.flatMap { it.values }.fold(zero) { acc, it -> acc plus (it.cost times it.flow) }
    }
}