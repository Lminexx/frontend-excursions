package com.example.projectexcursions.ui.utilies


class UsernameNotFoundException(message: String): Exception(message)

class ApproveExcursionException():
    Exception("Excursion not approved!")

class ExcursionsListException:
        Exception()