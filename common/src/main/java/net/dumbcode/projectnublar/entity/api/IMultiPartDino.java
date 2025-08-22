package net.dumbcode.projectnublar.entity.api;

public interface IMultiPartDino {
    IDinoPart[] getParts();
    boolean hurtFromPart(IDinoPart part, float amount);
}
