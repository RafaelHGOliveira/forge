package forge.toolbox.special;

import javax.swing.JPanel;

import forge.game.phase.PhaseType;
import forge.util.Localizer;
import net.miginfocom.swing.MigLayout;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public class PhaseIndicator extends JPanel {
    private static final long serialVersionUID = -863730022835609252L;
    
    // Phase labels
    private PhaseLabel lblUpkeep = new PhaseLabel("UP");
    private PhaseLabel lblDraw = new PhaseLabel("DR");
    private PhaseLabel lblMain1 = new PhaseLabel("M1");
    private PhaseLabel lblBeginCombat = new PhaseLabel("BC");
    private PhaseLabel lblDeclareAttackers = new PhaseLabel("DA");
    private PhaseLabel lblDeclareBlockers = new PhaseLabel("DB");
    private PhaseLabel lblFirstStrike = new PhaseLabel("FS");
    private PhaseLabel lblCombatDamage = new PhaseLabel("CD");
    private PhaseLabel lblEndCombat = new PhaseLabel("EC");
    private PhaseLabel lblMain2 = new PhaseLabel("M2");
    private PhaseLabel lblEndTurn = new PhaseLabel("ET");
    private PhaseLabel lblCleanup = new PhaseLabel("CL");
    
    
    public PhaseIndicator() { 
        this.setOpaque(false);
        this.setLayout(new MigLayout("insets 0 0 1% 0, gap 0, wrap"));
        populatePhase();
    }
    
    /** Adds phase indicator labels to phase area JPanel container. */
    private void populatePhase() {
        final Localizer localizer = Localizer.getInstance();
        // Each label: 6.5% height. Normal gap 1%, group separator gap 3%.
        // 12 * 6.5% + (8 * 1% + 4 * 3%) = 78% + 20% = 98% — fits in 100%.
        // Groups: Beginning (UP, DR) | Main1 | Combat (BC–EC) | Main2 | Ending (ET, CL)
        final String lbl  = "w 94%!, h 6.5%, gaptop 1%, gapleft 3%";
        final String grp  = "w 94%!, h 6.5%, gaptop 3%, gapleft 3%"; // first label of a new group

        // — Beginning phase —
        lblUpkeep.setToolTipText(localizer.getMessage("htmlPhaseUpkeepTooltip"));
        this.add(lblUpkeep, lbl);

        lblDraw.setToolTipText(localizer.getMessage("htmlPhaseDrawTooltip"));
        this.add(lblDraw, lbl);

        // — Main Phase 1 —
        lblMain1.setToolTipText(localizer.getMessage("htmlPhaseMain1Tooltip"));
        this.add(lblMain1, grp);

        // — Combat phase —
        lblBeginCombat.setToolTipText(localizer.getMessage("htmlPhaseBeginCombatTooltip"));
        this.add(lblBeginCombat, grp);

        lblDeclareAttackers.setToolTipText(localizer.getMessage("htmlPhaseDeclareAttackersTooltip"));
        this.add(lblDeclareAttackers, lbl);

        lblDeclareBlockers.setToolTipText(localizer.getMessage("htmlPhaseDeclareBlockersTooltip"));
        this.add(lblDeclareBlockers, lbl);

        lblFirstStrike.setToolTipText(localizer.getMessage("htmlPhaseFirstStrikeDamageTooltip"));
        this.add(lblFirstStrike, lbl);

        lblCombatDamage.setToolTipText(localizer.getMessage("htmlPhaseCombatDamageTooltip"));
        this.add(lblCombatDamage, lbl);

        lblEndCombat.setToolTipText(localizer.getMessage("htmlPhaseEndCombatTooltip"));
        this.add(lblEndCombat, lbl);

        // — Main Phase 2 —
        lblMain2.setToolTipText(localizer.getMessage("htmlPhaseMain2Tooltip"));
        this.add(lblMain2, grp);

        // — Ending phase —
        lblEndTurn.setToolTipText(localizer.getMessage("htmlPhaseEndTurnTooltip"));
        this.add(lblEndTurn, grp);

        lblCleanup.setToolTipText(localizer.getMessage("htmlPhaseCleanupTooltip"));
        this.add(lblCleanup, lbl);
    }
    

    //========== Custom class handling
    public PhaseLabel getLabelFor(final PhaseType s) {
        switch (s) {
            case UPKEEP:
                return this.getLblUpkeep();
            case DRAW:
                return this.getLblDraw();
            case MAIN1:
                return this.getLblMain1();
            case COMBAT_BEGIN:
                return this.getLblBeginCombat();
            case COMBAT_DECLARE_ATTACKERS:
                return this.getLblDeclareAttackers();
            case COMBAT_DECLARE_BLOCKERS:
                return this.getLblDeclareBlockers();
            case COMBAT_DAMAGE:
                return this.getLblCombatDamage();
            case COMBAT_FIRST_STRIKE_DAMAGE:
                return this.getLblFirstStrike();
            case COMBAT_END:
                return this.getLblEndCombat();
            case MAIN2:
                return this.getLblMain2();
            case END_OF_TURN:
                return this.getLblEndTurn();
            case CLEANUP:
                return this.getLblCleanup();
            default:
                return null;
        }
    }

    /**
     * Resets all phase buttons to "inactive", so highlight won't be drawn on
     * them. "Enabled" state remains the same.
     */
    public void resetPhaseButtons() {
        getLblUpkeep().setActive(false);
        getLblDraw().setActive(false);
        getLblMain1().setActive(false);
        getLblBeginCombat().setActive(false);
        getLblDeclareAttackers().setActive(false);
        getLblDeclareBlockers().setActive(false);
        getLblFirstStrike().setActive(false);
        getLblCombatDamage().setActive(false);
        getLblEndCombat().setActive(false);
        getLblMain2().setActive(false);
        getLblEndTurn().setActive(false);
        getLblCleanup().setActive(false);
    }

    // Phases
    /** @return {@link javax.swing.JLabel} */
    public PhaseLabel getLblUpkeep() {
        return this.lblUpkeep;
    }

    /** @return {@link javax.swing.JLabel} */
    public PhaseLabel getLblDraw() {
        return this.lblDraw;
    }

    /** @return {@link javax.swing.JLabel} */
    public PhaseLabel getLblMain1() {
        return this.lblMain1;
    }

    /** @return {@link javax.swing.JLabel} */
    public PhaseLabel getLblBeginCombat() {
        return this.lblBeginCombat;
    }

    /** @return {@link javax.swing.JLabel} */
    public PhaseLabel getLblDeclareAttackers() {
        return this.lblDeclareAttackers;
    }

    /** @return {@link javax.swing.JLabel} */
    public PhaseLabel getLblDeclareBlockers() {
        return this.lblDeclareBlockers;
    }

    /** @return {@link javax.swing.JLabel} */
    public PhaseLabel getLblCombatDamage() {
        return this.lblCombatDamage;
    }

    /** @return {@link javax.swing.JLabel} */
    public PhaseLabel getLblFirstStrike() {
        return this.lblFirstStrike;
    }

    /** @return {@link javax.swing.JLabel} */
    public PhaseLabel getLblEndCombat() {
        return this.lblEndCombat;
    }

    /** @return {@link javax.swing.JLabel} */
    public PhaseLabel getLblMain2() {
        return this.lblMain2;
    }

    /** @return {@link javax.swing.JLabel} */
    public PhaseLabel getLblEndTurn() {
        return this.lblEndTurn;
    }

    /** @return {@link javax.swing.JLabel} */
    public PhaseLabel getLblCleanup() {
        return this.lblCleanup;
    }
}