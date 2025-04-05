package com.example.projectexcursions


class UsernameNotFoundException(message: String): Exception(message)

class ApproveExcursionException():
    Exception("Excursion not approved!")