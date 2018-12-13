package com.hardrockrealms.warsftbdynmap.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper class which holds a chunk location, and from a list of available chunks finds adjacent chunks to this
 * one. This is used in the process of grouping chunks together.
 */

class AdjacentChunk {
    private ChunkPosition loc;

    private AdjacentChunk m_top = null;
    private AdjacentChunk m_bottom = null;
    private AdjacentChunk m_left = null;
    private AdjacentChunk m_right = null;

    AdjacentChunk(ChunkPosition pos) {
        loc = pos;
    }

    /**
     * @return Returns the chunk location, X, Z and Dimension
     */

    ChunkPosition getPos() {
        return loc;
    }

    /**
     * @return Return a list of chunk edges that are open and not attached to another chunk.
     */

    List<ChunkEdge> getOpenChunkEdges() {
        List<ChunkEdge> edges = new ArrayList<>();

        if (m_top == null) {
            edges.add(new ChunkEdge(this, ChunkEdge.Edge.TOP));
        }

        if (m_bottom == null) {
            edges.add(new ChunkEdge(this, ChunkEdge.Edge.BOTTOM));
        }

        if (m_left == null) {
            edges.add(new ChunkEdge(this, ChunkEdge.Edge.LEFT));
        }

        if (m_right == null) {
            edges.add(new ChunkEdge(this, ChunkEdge.Edge.RIGHT));
        }

        return edges;
    }

    /**
     * Takes a list of available claim chunks and finds all chunks which attach to this chunk location. As chunks
     * are found to be attached they are removed from the availableChunks list and put it the processedChunks list.
     *
     * Note: This is a recursive method.
     *
     * @param availableChunks A list of available chunks to search for adjacent chunks
     * @param processedChunks A list of chunks that have been found and processed already.
     */

    void processAdjacentChunks(Set<ChunkPosition> availableChunks, Map<ChunkPosition, AdjacentChunk> processedChunks) {
        ChunkPosition[] adjacentPos = { new ChunkPosition(loc.posX, loc.posZ - 1, loc.dim),
                                        new ChunkPosition(loc.posX, loc.posZ + 1, loc.dim),
                                        new ChunkPosition(loc.posX - 1, loc.posZ, loc.dim),
                                        new ChunkPosition(loc.posX + 1, loc.posZ, loc.dim)
        };

        AdjacentChunk[] adjacentChunks = { null, null, null, null };

        // Loop through all 4 sides of the chunk and associate each neighbor
        for (int index = 0; index < 4; index++)
        {
            if (availableChunks.contains(adjacentPos[index])) {
                // Once we process a chunk remove it from the available chunk map so we don't keep processing over it
                availableChunks.remove(adjacentPos[index]);

                adjacentChunks[index] = new AdjacentChunk(adjacentPos[index]);
                processedChunks.put(adjacentPos[index], adjacentChunks[index]);

                adjacentChunks[index].processAdjacentChunks(availableChunks, processedChunks);
            }
            else {
                // Associate the side if it exists in the processed chunk list.
                adjacentChunks[index] = processedChunks.get(adjacentPos[index]);
            }
        }

        m_top = adjacentChunks[0];
        m_bottom = adjacentChunks[1];
        m_left = adjacentChunks[2];
        m_right = adjacentChunks[3];
    }

    /**
     * Clean up all the cross references so the garbage collector can destroy the objects
     */

    void cleanup() {
        loc = null;
        m_top = null;
        m_bottom = null;
        m_left = null;
        m_right = null;
    }
}
