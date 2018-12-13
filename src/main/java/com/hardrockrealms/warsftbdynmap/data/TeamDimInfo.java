package com.hardrockrealms.warsftbdynmap.data;

/**
 * Helper method to hold FTB team information and dimension index. Used for passing around what dimension and
 * team had a claim update.
 */

public class TeamDimInfo {
    private String m_teamTitle;
    private String m_teamDescription;

    private int m_teamColor;
    private String m_teamID;
    private int m_dim;

    /**
     * Initializes the team and dimension information
     * @param teamID The id of the FTB claim team
     * @param dim The dimension this information is associated with
     * @param title The FTB team title
     * @param description The FTB team description
     * @param rgbColor The FTB team color
     */

    public TeamDimInfo(String teamID, int dim, String title, String description, int rgbColor) {
        m_teamID = teamID;
        m_dim = dim;
        m_teamTitle = title;
        m_teamDescription = description;
        m_teamColor = rgbColor;
    }

    /**
     * @return Returns the FTB team ID
     */

    public String getTeamID() {
        return m_teamID;
    }

    /**
     * @return Returns the Dimension associated with this object
     */

    public int getDim() {
        return m_dim;
    }

    /**
     * @return Returns the FTB team color
     */

    public int getTeamColor() {
        return m_teamColor;
    }

    /**
     * @return Returns the FTB team description
     */

    public String getTeamDescription() {
        return m_teamDescription;
    }

    /**
     * @return Returns the FTB team title
     */

    public String getTeamTitle() {
        return m_teamTitle;
    }

    /**
     * Compares the object to this object
     * @param obj The object to compare this object too
     * @return Returns true if the object is the same type and the contents match.
     */

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else {
            return obj instanceof TeamDimInfo && this.equals((TeamDimInfo) obj);
        }
    }

    /**
     * Compares this object to another TeamDimInfo object.
     * @param teamDim Other object to compare against
     * @return Returns true if the team name and dimension match, all other data is meta data and not used in the
     * comparison.
     */

    private boolean equals(TeamDimInfo teamDim) {
        if (teamDim == null) {
            return false;
        } else {
            return m_teamID == teamDim.m_teamID && m_dim == teamDim.m_dim;
        }
    }

    /**
     * @return Returns a unique hashcode for this object when comparing against other objects of this type.
     */

    public int hashCode() {
        return m_teamID.hashCode() + m_dim;
    }

}
