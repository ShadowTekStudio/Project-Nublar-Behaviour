package net.dumbcode.projectnublar.entity.api;


import net.dumbcode.projectnublar.entity.Dinosaur;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;
import java.util.UUID;

public interface IPackBehavior {

    public Optional<UUID> getPackId();
    public Optional<UUID> getPackLeader();
    public void createNewPack(ServerLevel level,Dinosaur dinosaur);
    public void registerWithPack(Dinosaur dinosaur);
    public void setPackLeader(Optional<UUID> leaderID);
    public void setPackID(Optional<UUID> packID);

}
