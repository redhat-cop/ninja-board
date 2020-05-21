package com.redhat.services.ninja.mock;

import com.redhat.services.ninja.service.LdapService;
import com.redhat.services.ninja.user.RedHatUser;
import io.quarkus.test.Mock;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

@Mock
@ApplicationScoped
public class MockLdapService extends LdapService {

    @Override
    protected void init() {
    }

    @Override
    public List<RedHatUser> search(String key, String value) {
        if (key.equals("uid") && value.equals("new_ninja")) {
            RedHatUser user = RedHatUser.newMapper().apply(Map.of(
                    "uid", "new_ninja",
                    "employeeNumber", "new_ninja",
                    "mail", "new_ninja@redhat.com",
                    "mobile", "8888888888",
                    "rhatLocation", "New York",
                    "rhatJobTitle", "Consultant",
                    "title", "Consultant",
                    "rhatGeo", "EMEA",
                    "rhatHireDate", "1/1/2020",
                    "rhatJobCode", "14"
                    )
            );
            return List.of(user);
        } else
            return List.of();
    }
}
