package com.hardrockrealms.warsftbdynmap.integrations;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import com.hardrockrealms.warsftbdynmap.data.ChunkPosition;
import com.hardrockrealms.warsftbdynmap.data.TeamDimInfo;

import java.util.*;

/**
 * This is a interface class to FTBUtilities, all calls that interface to FTBUtilities should come through here.
 */

public class FTBUtilitiesIntegration {

    /**
     * @param teamDim The team info you want the claim list for
     * @return Returns a list of all chunks a team has claimed in all dimensions
     */

    public static Set<ChunkPosition> getTeamClaimedChunks(TeamDimInfo teamDim) {
        Collection<ClaimedChunk> ftbClaimedChunks = ClaimedChunks.instance.getAllChunks();
        Set<ChunkPosition> claimedChunkPositions = new HashSet<>();
        ForgeTeam team = Universe.get().getTeam(teamDim.getTeamID());

        for (ClaimedChunk chunk : ftbClaimedChunks) {
            if (!chunk.isInvalid() && chunk.getPos().dim == teamDim.getDim() && team.equalsTeam(chunk.getTeam())) {
                claimedChunkPositions.add(new ChunkPosition(chunk.getPos().posX, chunk.getPos().posZ, chunk.getPos().dim));
            }
        }

        return claimedChunkPositions;
    }

    /**
     * @return Returns a list of all teams in FTB which have claims associated with them.
     */

    public static Set<TeamDimInfo> getListTeamDimWithClaims() {
        Collection<ClaimedChunk> ftbClaimedChunks = ClaimedChunks.instance.getAllChunks();
        Set<TeamDimInfo> teamDimList = new HashSet<>();

        for (ClaimedChunk chunk : ftbClaimedChunks) {
            TeamDimInfo teamDim = new TeamDimInfo(chunk.getTeam().toString(),
                    chunk.getPos().dim,
                    chunk.getTeam().getTitle().getUnformattedText(),
                    chunk.getTeam().getDesc(),
                    chunk.getTeam().getColor().getColor().rgba() & 0x00FFFFFF);

            teamDimList.add(teamDim);
        }

        return teamDimList;
    }

    public static List<String> getTeamMembers(String teamID) {
        ForgeTeam team = Universe.get().getTeam(teamID);
        List<String> teamMembers = new ArrayList<>();

        if (team != null) {
            for (ForgePlayer player: team.getMembers()) {
                teamMembers.add(player.toString());
            }
        }

        return teamMembers;
    }
}
