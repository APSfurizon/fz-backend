@param String username
@param String email
@param String currentYear

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registration Success</title>
    <link href="https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css" rel="stylesheet">
</head>
<body class="bg-gray-100">
<div class="max-w-lg mx-auto my-10 p-6 bg-white shadow-md rounded-lg">
    <h1 class="text-2xl font-bold text-gray-800 mb-4">Welcome to Our Platform, ${username}!</h1>

    <p class="text-gray-700">
        Congratulations! You have successfully registered on our platform. We’re excited to have you on board.
    </p>

    <div class="mt-6">
        <p class="text-gray-700">
            Here are your registration details:
        </p>
        <ul class="mt-4 space-y-2">
            <li><span class="font-medium text-gray-900">Username:</span> ${username}</li>
            <li><span class="font-medium text-gray-900">Email:</span> ${email}</li>
        </ul>
    </div>

    <div class="mt-8">
        <a href="#" class="px-4 py-2 bg-blue-500 text-white font-medium rounded hover:bg-blue-600">
            Go to Dashboard
        </a>
    </div>

    <p class="mt-8 text-sm text-gray-500">
        If you did not register on our platform, please ignore this email or contact our support team.
    </p>

    <footer class="mt-8 text-center text-xs text-gray-400">
        &copy; ${currentYear} Your Company Name. All rights reserved.
    </footer>
</div>
</body>
</html>
