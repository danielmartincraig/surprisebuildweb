resource "aws_cognito_identity_pool" "example" {
  identity_pool_name               = "example_identity_pool"
  allow_unauthenticated_identities = false
  allow_classic_flow               = false

  cognito_identity_providers {
    client_id               = aws_cognito_user_pool_client.example.client_id
    provider_name           = aws_cognito_user_pool.example.endpoint
    server_side_token_check = true
  }
}

resource "aws_cognito_user_pool" "example" {
  name = "example_user_pool"
}

resource "aws_cognito_user_pool_client" "example" {
  name         = "example_user_pool_client"
  user_pool_id = aws_cognito_user_pool.example.id
}