package io.rolique.roliqueapp.screens.navigation.contacts;

import dagger.Component;
import io.rolique.roliqueapp.RoliqueApplicationComponent;
import io.rolique.roliqueapp.screens.ViewScope;

/**
 * Created by Volodymyr Oleshkevych on 9/12/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@ViewScope
@Component(dependencies = RoliqueApplicationComponent.class)
interface ContactsComponent {

    void inject(ContactsFragment contactsFragment);
}
