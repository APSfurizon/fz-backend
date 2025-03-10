package net.furizon.backend.feature.nosecount.usecase;

import lombok.RequiredArgsConstructor;
import net.furizon.backend.feature.nosecount.dto.AdminCountRole;
import net.furizon.backend.feature.nosecount.dto.JooqAdmincountObj;
import net.furizon.backend.feature.nosecount.dto.responses.AdminCountResponse;
import net.furizon.backend.feature.nosecount.finder.CountsFinder;
import net.furizon.backend.feature.user.dto.UserDisplayData;
import net.furizon.backend.infrastructure.usecase.UseCase;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LoadAdminCountUseCase implements UseCase<Integer, AdminCountResponse> {
    @NotNull private final CountsFinder countsFinder;

    @Override
    public @NotNull AdminCountResponse executor(@NotNull Integer input) {
        List<JooqAdmincountObj> data = countsFinder.getAdmins();

        List<AdminCountRole> roles = new LinkedList<>();
        Map<Long, List<UserDisplayData>> roleIdToMembers = new HashMap<>();

        for (JooqAdmincountObj admincountObj : data) {
            List<UserDisplayData> members = roleIdToMembers.computeIfAbsent(
                admincountObj.getRoleId(),
                roleId -> {
                    List<UserDisplayData> l = new ArrayList<>();
                    String displayName = admincountObj.getRoleDisplayName();
                    AdminCountRole role = new AdminCountRole(
                        displayName == null ? admincountObj.getRoleInternalName() : displayName,
                        l
                    );
                    roles.add(role);
                    return l;
                }
            );

            members.add(admincountObj.getUser());
        }

        for (List<UserDisplayData> users : roleIdToMembers.values()) {
            users.sort(((o1, o2) -> (int) (o1.getUserId() - o2.getUserId())));
        }

        return new AdminCountResponse(roles);
    }
}
